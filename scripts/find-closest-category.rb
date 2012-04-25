#! /usr/bin/ruby -w

#
# given "./find-closest-category.rb 'Alain Delon' acteur écrivain plombier" this script should say 'acteur'
#


class ScoredCategory
    attr_accessor :name, :score
    
    def initialize(name, score)
        @name = name
        @score = score
    end
    
    def to_s
        "#{@name}(#{@score})"
    end
end


def main(args)
    require 'ngd/googlehits'
    require 'ngd/complexitycalculator'

    google = GoogleHits.new true, 'fr', '.fr'
    calc = ComplexityCalculator.new(google)
    
    x = args[0]
    concepts = args[1..args.length]
    throw RuntimeError.new("must provide an individual and some categories") if x == nil or concepts.length == 0

    sorted_categories = []
    
    c_x = calc.get_complexity(x)
    puts "#{x} : #{c_x}"    
    concepts.each { |concept| 
        c_c = calc.get_complexity(concept)
        
        puts "#{concept} : #{c_c}"
        
        c_x_c = calc.get_conditional_complexity(x, concept)
        puts "C(#{x}|#{concept}) : #{c_x_c}"
        
        #score = (c_c + c_x_c).to_f / c_c
        score = calc.get_ngd(x, concept)
        cat = ScoredCategory.new(concept, score)
        sorted_categories << cat
    }
    
    puts
    puts "**************************"
    puts sorted_categories.sort_by { |c| c.score }
end


main(ARGV)

