#! /usr/bin/ruby -w

#
# use it like this, for example: ./script actor famous-french-people.txt
#

require 'ngd/googlehits'

google_all = GoogleHits.new true
google_wiki = GoogleHits.new false, nil, 'wikipedia.org'
google_nytimes = GoogleHits.new false, nil, 'nytimes.com'

puts google_all.get_size, google_wiki.get_size, google_nytimes.get_size

#index_size_query = "a *"
#wiki_size = google_wiki.get_hits(index_size_query)
#ny_size = google_nytimes.get_hits(index_size_query)

puts google_all.get_hits('inurl:http')
puts google_all.get_hits('* a *')
