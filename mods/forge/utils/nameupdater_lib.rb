def processClassNames(mappings, file_name, stored_index)
  File.open(file_name, 'r') do |file|
    while (line = file.gets)

      # Try to match class name
      if (match = line.match(/CL: ([a-zA-Z]+) ([a-zA-Z0-9\/]+)/))
        obfuscated, unobfuscated = match.captures

        simpleClassName = unobfuscated.split("/").last
        classHash = mappings["classes"][simpleClassName]

        if classHash != nil
          javaName = classHash["mcp"].gsub('.', '/')

          if unobfuscated == javaName
            if obfuscated != classHash[stored_index]
              printf "class %s: %s => %s\n", simpleClassName, v["obf"], obfuscated
              mappings["classes"][simpleClassName][stored_index] = obfuscated
            else
              printf "class %s: unchanged\n", simpleClassName
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
end

def processMethodNames(mappings, file_name, stored_index)
  File.open(file_name, 'r') do |file|
    while (line = file.gets)

      # Try to match method name
      if (match = line.match(/MD: ([a-zA-Z]+)\/([a-zA-Z]+) ([()A-Za-z0-9;\/]+) ([a-zA-Z0-9\/]+)\/([a-zA-Z0-9_]+) ([()A-Za-z0-9;\/]+)/))
        obfuscatedClass, obfuscated, signature, unobfuscatedClass, methodName, signature2 = match.captures

        simpleClassName = unobfuscatedClass.split("/").last
        methodHash = mappings["methods"][simpleClassName]

        if methodHash != nil && methodHash.key?(methodName)
          parsedSignature = methodHash[methodName]['signature']

          # replace simple symbols in the signature with the obfuscated or unobfuscated variant
          if (parsedSignature.include? '#')
            mappings["classes"].each_pair do |c_class_name, c_v|
              parsedSignature = parsedSignature.gsub('#' + c_class_name + ';', c_v[stored_index] + ';')
            end
          end

          # ToDo convert class signatures to #Signature ?

          if methodHash[methodName] == nil or methodHash[methodName][stored_index] != obfuscated
            storedObfuscatedName = !methodHash[methodName].key?(stored_index) ? '(new)' : methodHash[methodName][stored_index]

            print methodHash
            print "\n"
            printf "method %s/%s %s: %s=%s => %s\n", simpleClassName, methodName, signature, parsedSignature, storedObfuscatedName, obfuscated
            mappings["methods"][simpleClassName][methodName][stored_index] = obfuscated
          else
            printf "method %s/%s %s=%s: unchanged\n", simpleClassName, methodName, signature, parsedSignature
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
end

def processFieldNames(mappings, file_name, target_key)
  File.open(file_name, 'r') do |file|
    while (line = file.gets)

      # Try to match method name
      if (match = line.match(/FD: ([a-zA-Z0-9\/]+)\/([a-zA-Z0-9_]+) ([a-zA-Z0-9\/]+)\/([a-zA-Z0-9_]+)/))
        obfuscatedClass, obfuscated, unobfuscatedClass, fieldName = match.captures

        simpleClassName = unobfuscatedClass.split("/").last
        fieldHash = mappings["fields"][simpleClassName]

        if fieldHash != nil && fieldHash.key?(fieldName)
          if fieldHash[fieldName][target_key] != obfuscated
            printf "field %s/%s: %s => %s\n", simpleClassName, fieldName, fieldHash[target_key], obfuscated
            mappings["fields"][simpleClassName][fieldName][target_key] = obfuscated
          else
            printf "field %s/%s: unchanged\n", simpleClassName, fieldName
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
end