def processClassNames(mappings, file_name, stored_index)
  File.open(file_name, 'r') do |file|
    while (line = file.gets)

      # Try to match class name
      if (match = line.match(/CL: ([a-zA-Z]+) ([a-zA-Z0-9\/]+)/))
        obfuscated, unobfuscated = match.captures

        mappings["classes"].each_pair do |klass, v|
          javaName = v["mcp"].gsub(".", "/")

          if unobfuscated == javaName
            if obfuscated != v[stored_index]
              printf "class %s: %s => %s\n", klass, v["obf"], obfuscated
              mappings["classes"][klass][stored_index] = obfuscated
            else
              printf "class %s: unchanged\n", klass
            end
          end
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

        mappings["methods"].each_pair do |klass, v|
          fullClassName = mappings["classes"][klass]["mcp"]
          javaName = fullClassName.gsub(".", "/")

          if unobfuscatedClass == javaName
            v.each_pair do |mName, z|
              obfuscatedName = z[stored_index]
              storedSignature = z["signature"]
              originalSignature = storedSignature

              # replace simple symbols in the signature with the obfuscated or unobfuscated variant
              if (storedSignature.include? '#')
                mappings["classes"].each_pair do |c_class_name, c_v|
                  storedSignature = storedSignature.gsub('#' + c_class_name + ';', c_v[stored_index] + ';')
                end
              end

              if methodName == mName and storedSignature == signature
                if obfuscated != obfuscatedName
                  printf "method %s/%s %s: %s=%s => %s\n", klass, methodName, originalSignature, storedSignature, obfuscatedName, obfuscated
                  mappings["methods"][klass][methodName][stored_index] = obfuscated
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
end

def processFieldNames(mappings, file_name, target_key)
  File.open(file_name, 'r') do |file|
    while (line = file.gets)

      # Try to match method name
      if (match = line.match(/FD: ([a-zA-Z0-9\/]+)\/([a-zA-Z0-9_]+) ([a-zA-Z0-9\/]+)\/([a-zA-Z0-9_]+)/))
        obfuscatedClass, obfuscated, unobfuscatedClass, fieldName = match.captures

        mappings["fields"].each_pair do |klass, v|
          fullClassName = mappings["classes"][klass]["mcp"]
          javaName = fullClassName.gsub(".", "/")

          if unobfuscatedClass == javaName
            v.each_pair do |fName, store|
              if fieldName == fName
                if obfuscated != store[target_key]
                  printf "field %s/%s: %s => %s\n", klass, fieldName, store[target_key], obfuscated
                  mappings["fields"][klass][fieldName][target_key] = obfuscated
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
end