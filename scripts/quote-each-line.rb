#! /usr/bin/ruby

# take a text file and output lines, quoted

file = File.new(ARGV[0], "r")
file.each_line do |line|
    print "'#{line.chomp}'' "
end

