#!/usr/bin/env ruby

require 'yaml'

# Location of the srg mappings file
SRG_FILE="../mcp/temp/client_ro.srg"

# Location of the mappings file we need to lookup
RESOURCES_FOLDER="../src/main/resources/"
MAP_FILE="#{RESOURCES_FOLDER}mappings.yml"

puts "==== This is the LWC ASM mapping updater."
puts "  == This pulls from client_ro.srg to get updated names so ensure MCP/Forge have been updated!"
puts

server_version=`grep ServerVersion ../mcp/conf/version.cfg | sed 's/ServerVersion = //g'`
version_found = false

if server_version =~ /[a-zA-Z0-9.]+/
  server_version = server_version.split.join(' ')
  puts "==== Detected Minecraft version: " + server_version
  version_found = true
else
  puts "==== Unable to detect Minecraft version. This may just mean MCP's version.cfg changed which is OK."
  puts "==== This means that mappings_MCVERSION.yml will not be automatically generated."
end

file_exists = File.exists?(SRG_FILE)

puts "==== client_ro.srg detected: " + (file_exists ? File.expand_path(SRG_FILE) : "NOT FOUND")

if ! file_exists
  puts "NO client_ro.srg FOUND! Ensure this exists and MCP/Forge is setup+updated correctly: " + SRG_FILE
  exit
end

file_exists = File.exists?(MAP_FILE)

puts "==== mappings.yml detected: " + (file_exists ? File.expand_path(MAP_FILE) : "NOT FOUND")

if ! file_exists
  puts "NO mappings.yml FOUND! Ensure this exists and LWC is setup correctly: " + MAP_FILE
  exit
end

puts

# Open the current mappings file
mappings = YAML::load( File.open(MAP_FILE) )

puts "  == pass 1 - class names"

File.open(SRG_FILE, 'r') do |file|
  while (line = file.gets)

    # Try to match class name
    if (match = line.match(/CL: ([a-zA-Z]+) ([a-zA-Z0-9\/]+)/))
      obfuscated, unobfuscated = match.captures

      mappings["classes"].each_pair do |klass, v|
        javaName = v["mcp"].gsub(".", "/")

        if unobfuscated == javaName
          if obfuscated != v["obf"]
            printf "class %s: %s => %s\n", klass, v["obf"], obfuscated
            mappings["classes"][klass]["obf"] = obfuscated
          else
            printf "class %s: unchanged\n", klass
          end
        end
      end
    end

  end
end

puts "\n  == pass 2 - method names"

File.open(SRG_FILE, 'r') do |file|
  while (line = file.gets)

    # Try to match method name
    if (match = line.match(/MD: ([a-zA-Z]+)\/([a-zA-Z]+) ([()A-Za-z0-9;\/]+) ([a-zA-Z0-9\/]+)\/([a-zA-Z0-9_]+) ([()A-Za-z0-9;\/]+)/))
      obfuscatedClass, obfuscated, signature, unobfuscatedClass, methodName, signature2 = match.captures

      mappings["methods"].each_pair do |klass, v|
        fullClassName = mappings["classes"][klass]["mcp"]
        javaName = fullClassName.gsub(".", "/")

        if unobfuscatedClass == javaName
          v.each_pair do |mName, z|
            obfuscatedName = z["obf"]
            storedSignature = z["signature"]
            originalSignature = storedSignature

            # replace simple symbols in the signature with the obfuscated or unobfuscated variant
            if (storedSignature.include? '#')
              mappings["classes"].each_pair do |c_class_name, c_v|
                storedSignature = storedSignature.gsub('#' + c_class_name + ';', c_v["obf"] + ';')
              end
            end

            if methodName == mName and storedSignature == signature
              if obfuscated != obfuscatedName
                printf "method %s/%s %s: %s=%s => %s\n", klass, methodName, originalSignature, storedSignature, obfuscatedName, obfuscated
                mappings["methods"][klass][methodName]["obf"] = obfuscated
              else
                printf "method %s/%s %s=%s: unchanged\n", klass, methodName, originalSignature, storedSignature
              end
            end
          end
        end
      end
    end

  end
end

puts "\n  == pass 3 - field names"

File.open(SRG_FILE, 'r') do |file|
  while (line = file.gets)

    # Try to match method name
    if (match = line.match(/FD: ([a-zA-Z]+)\/([a-zA-Z]+) ([a-zA-Z0-9\/]+)\/([a-zA-Z0-9_]+)/))
      obfuscatedClass, obfuscated, unobfuscatedClass, fieldName = match.captures

      mappings["fields"].each_pair do |klass, v|
        fullClassName = mappings["classes"][klass]["mcp"]
        javaName = fullClassName.gsub(".", "/")

        if unobfuscatedClass == javaName
          v.each_pair do |fName, obfuscatedName|
            if fieldName == fName
              if obfuscated != obfuscatedName
                printf "field %s/%s: %s => %s\n", klass, fieldName, obfuscatedName, obfuscated
                mappings["fields"][klass][fieldName] = obfuscated
              else
                printf "field %s/%s: unchanged\n", klass, fieldName
              end
            end
          end
        end
      end
    end

  end
end

puts "\n==== Done."
puts "\n==== Dumping results to mappings.yml"

File.open(MAP_FILE, "w") do |file|
  file.write mappings.to_yaml
end

if version_found
  generate_file="#{RESOURCES_FOLDER}mappings_#{server_version}.yml"

  puts "==== Dumping results to mappings_#{server_version}.yml"

  File.open(generate_file, "w") do |file|
    file.write mappings.to_yaml
  end
end