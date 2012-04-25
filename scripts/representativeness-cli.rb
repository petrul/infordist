#! /usr/bin/ruby -w

require 'representativeness'

$priors = 

def main
    #d1 = 'HHHHH'
    #d2 = 'TTTTT'

    hypname = :h_fair
    #hypname = :h_weighted
    #hypname = :h_heads_only
    puts "hypothesis: #{hypname}"
    
    h = $hypothesis[hypname]
    puts "P(H|#{hypname}) = " + h.call('H').to_s
    puts "P(T|#{hypname}) = " + h.call('T').to_s
    
#    puts likelihood(d1, hypname)
#    puts likelihood(d2, hypname)
    
    call_repr('HHHHH', :h_fair)
    call_repr('TTTTT', :h_fair)
    
    puts "=" * 20 
    
    call_repr('HHHTT', :h_fair)
    call_repr('HTHHT', :h_fair)
    
end


def call_repr(d, h)
    priors = {
            :h_fair => 0.6,
            :h_weighted => 0.3,
            :h_heads_only => 0.1
        }
    puts "repr(#{d}, #{h}) = " + repr(d, h, priors).to_s
end

main()