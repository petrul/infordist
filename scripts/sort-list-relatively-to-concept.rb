#! /usr/bin/ruby -w

#
# use it like this, for example: ./script actor famous-french-people.txt
#


MAX_PEOPLE = 20 * 1000 


class Person
    attr_accessor :name, :score
    
    def initialize(name, score)
        @name = name
        @score = score
    end
    
    def to_s
        @name + " (" + @score.to_s + "), "
    end
end


#
#
#
def main(args)
    require 'ngd/googlehits'
    require 'ngd/complexitycalculator'

    google = GoogleHits.new
    calc = ComplexityCalculator.new(google)

    concept = args[0]
    filename = args[1]


    puts "will compare list of people in #{filename} to concept '#{concept}'"
    
    people = []
    counter = 0
    File.new(filename).each_line do |line|
        line = line.strip
        ngd = 1.0
        
        begin
            dist = calc.get_ngd(line, concept)
            #dist = calc.get_conditional_complexity(line, concept)
        rescue RuntimeError
            $stderr.print "error while calculating distance : " + $!
        end
        #puts "#{line} => #{ngd}"

        if dist != nil then
            person = Person.new(line, dist) 
            people << person
        end
        
        if counter % 10 == 0 then print "#{counter}... " ; STDOUT.flush; end
        break if counter > MAX_PEOPLE
        counter = counter + 1
    end

    puts
    people = people.sort_by { |p| p.score }
    puts people
    export_to_html(concept, people)
    #puts people
end


#
#
#
def export_to_html(concept, people)
    File.open("#{concept}.html", 'w') do |f|
        f.puts("<h3>#{concept}</h3>");
        f.puts("<meta content='text/html; charset=UTF-8' http-equiv='Content-Type'/>")
        f.puts("<table>");
        f.puts("<tr><td>Name</td><td>Distance</td></tr>")
        
        people.each do |p|
            f.puts "<tr><td>#{p.name}</td><td>#{p.score}</td></tr>"
        end
        
        f.puts("</table>")
    end
end


main(ARGV)

