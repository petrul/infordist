#! /usr/bin/ruby -w

# 
# gets the number of google hits of the given query per internet sub-site (like a google search "haiti site:nytimes.com")
#

def main(args)
    require 'ngd/googlehits'
    require 'ngd/complexitycalculator'

    raise RuntimeError.new('need at least one argument, normally two: [query] [subsite]') if args.length < 1 
    query = args[0]
    subsite = args[1]
    
    google = GoogleHits.new(false, nil, subsite)
    #calc = ComplexityCalculator.new(google)
    
    hits = google.get_hits(query)
    
    puts "[#{Time.new}] '#{query}' #{subsite} #{hits}"
end

main(ARGV)
