#! /usr/bin/ruby 
require 'ngd/googlehits'

google = GoogleHits.new
csv = File.open("years.csv", "w")

for year in (1000..2100)
    query = "year " + year.to_s
    hits = google.get_hits(query)

    puts query + " => " + hits
    csv.puts(year.to_s + "; " + hits)
end

csv.close
