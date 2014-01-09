require 'colorize'

def processClassNames(mappings, file_name, stored_index)
  looked_at = []

  File.open(file_name, 'r') do |file|
    while (line = file.gets)

      # Try to match class name
      if match = line.match(/CL: ([a-zA-Z0-9$\/]+) ([a-zA-Z]+)/)
        unobfuscated, obfuscated = match.captures

        simpleClassName = unobfuscated.split("/").last
        classHash = mappings["classes"][simpleClassName]

        if classHash != nil
          javaName = classHash["mcp"].gsub('.', '/')

          if unobfuscated == javaName
            looked_at.push(simpleClassName)

            if obfuscated != classHash[stored_index]
              printf "class %s: %s => %s\n", simpleClassName, classHash["obf"], obfuscated.clone.colorize(:green)
              mappings["classes"][simpleClassName][stored_index] = obfuscated
            else
              printf "class %s: %s\n", simpleClassName, "unchanged".colorize(:yellow)
            end
          end
        else
#          printf "class %s: %s\n", simpleClassName, obfuscated
#          mappings["classes"][simpleClassName] = {
#              'mcp' => unobfuscated.gsub("/", "."),
#              stored_index => obfuscated
#          }
        end
      end

    end
  end

  mappings["classes"].each do |simpleClassName, dict|
    if not looked_at.include?(simpleClassName) and dict["mcp"] != dict["obf"]
      printf "class %s: %s\n", simpleClassName, "not matched".colorize(:red)
    end
  end
end

def processMethodNames(mappings, file_name, stored_index)
  looked_at = []

  File.open(file_name, 'r') do |file|
    while line = file.gets

      # Try to match method name
      if match = line.match(/([a-zA-Z0-9$\/]+)\/([a-zA-Z0-9_]+) ([()A-Za-z0-9;\/]+) ([a-zA-Z0-9$\/]+)\/([a-zA-Z0-9_]+) ([()A-Za-z0-9;\/]+)/)
        unobfuscatedClass, methodName, signature, obfuscatedClass, obfuscated, signature2 = match.captures

        simpleClassName = unobfuscatedClass.split("/").last
        methodHash = mappings["methods"][simpleClassName]

        if methodHash != nil && methodHash.key?(methodName)
          parsedSignature = methodHash[methodName]['signature']

          # replace simple symbols in the signature with the obfuscated or unobfuscated variant
          if (parsedSignature.include? '#')
            mappings["classes"].each_pair do |c_class_name, c_v|
              parsedSignature = parsedSignature.gsub('#' + c_class_name + ';', c_v["obf"] + ';')
            end
          end

          # ToDo convert class signatures to #Signature ?
          looked_at.push(simpleClassName + "/" + methodName)

          if methodHash[methodName] == nil or methodHash[methodName][stored_index] != obfuscated
            storedObfuscatedName = !methodHash[methodName].key?(stored_index) ? '(new)' : methodHash[methodName][stored_index]

            printf "method %s/%s %s: %s=%s => %s\n", simpleClassName, methodName, signature, parsedSignature, storedObfuscatedName, obfuscated.clone.colorize(:green)
            mappings["methods"][simpleClassName][methodName][stored_index] = obfuscated
          else
            printf "method %s/%s %s=%s: %s\n", simpleClassName, methodName, signature, parsedSignature, "unchanged".colorize(:yellow)
          end
        else
#          klass = unobfuscatedClass.split("/").last
#          printf "method %s/%s %s = %s\n", klass, methodName, signature, obfuscated
#          mappings["methods"][klass] = {
#              methodName => {
#                  stored_index => obfuscated,
#                  'signature' => signature
#              }
#          }
        end
      end

    end
  end

  mappings["methods"].each do |simpleClassName, dict|
    mappings["methods"][simpleClassName].each do |methodName, dict2|
      key = "%s/%s" % [simpleClassName, methodName]

      if not looked_at.include?(key) and dict2["signature"] != "n/a"
        printf "method %s: %s\n", key, "not matched".colorize(:red)
      end
    end
  end
end

def processFieldNames(mappings, file_name, target_key)
  looked_at = []

  File.open(file_name, 'r') do |file|
    while (line = file.gets)

      # Try to match method name
      if (match = line.match(/FD: ([a-zA-Z0-9$\/]+)\/([a-zA-Z0-9_]+) ([a-zA-Z0-9$\/]+)\/([a-zA-Z0-9_]+)/))
        unobfuscatedClass, fieldName, obfuscatedClass, obfuscated = match.captures

        simpleClassName = unobfuscatedClass.split("/").last
        fieldHash = mappings["fields"][simpleClassName]

        if fieldHash != nil && fieldHash.key?(fieldName)
          looked_at.push(simpleClassName + "/" + fieldName)

          if fieldHash[fieldName][target_key] != obfuscated
            printf "field %s/%s: %s => %s\n", simpleClassName, fieldName, fieldHash[target_key], obfuscated.clone.colorize(:green)
            mappings["fields"][simpleClassName][fieldName][target_key] = obfuscated
          else
            printf "field %s/%s: %s\n", simpleClassName, fieldName, "unchanged".colorize(:yellow)
          end
        else
#          printf "field %s/%s = %s\n", simpleClassName, fieldName, obfuscated
#          mappings["fields"][simpleClassName] = {
#              fieldName => {
#                  target_key => obfuscated
#              }
#          }
        end
      end

    end
  end

  mappings["fields"].each do |simpleClassName, dict|
    mappings["fields"][simpleClassName].each do |fieldName, dict2|
      key = "%s/%s" % [simpleClassName, fieldName]

      if not looked_at.include?(key)
        printf "field %s: %s\n", key, "not matched".colorize(:red)
      end
    end
  end
end