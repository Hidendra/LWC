#!/usr/bin/env ruby

require 'yaml'
require_relative 'nameupdater_lib'

# Location of the srg mappings file
SRG_FILE="../mcp/temp/client_ro.srg"
SRG_SRG_FILE="../mcp/temp/client_ro_srg.srg"

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

if !file_exists
  puts "NO client_ro.srg FOUND! Ensure this exists and MCP/Forge is setup+updated correctly: " + SRG_FILE
  exit
end

file_exists = File.exists?(SRG_SRG_FILE)

puts "==== client_ro_srg.srg detected: " + (file_exists ? File.expand_path(SRG_SRG_FILE) : "NOT FOUND")

if !file_exists
  puts "NO client_ro_srg.srg FOUND! Ensure this exists and MCP/Forge is setup+updated correctly: " + SRG_FILE
  exit
end

file_exists = File.exists?(MAP_FILE)

puts "==== mappings.yml detected: " + (file_exists ? File.expand_path(MAP_FILE) : "NOT FOUND")

if !file_exists
  puts "NO mappings.yml FOUND! Ensure this exists and LWC is setup correctly: " + MAP_FILE
  exit
end

puts

# Open the current mappings file
mappings = YAML::load(File.open(MAP_FILE))

puts "  == pass 1 - class names"

processClassNames(mappings, SRG_FILE, "obf")

puts "\n  == pass 2 - method names (obfuscated)"

processMethodNames(mappings, SRG_FILE, "obf")

puts "\n  == pass 3 - method names (srg)"

processMethodNames(mappings, SRG_SRG_FILE, "srg")

puts "\n  == pass 4 - field names (obfuscated)"

processFieldNames(mappings, SRG_FILE, "obf")

puts "\n  == pass 5 - field names (SRG)"

processFieldNames(mappings, SRG_SRG_FILE, "srg")

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