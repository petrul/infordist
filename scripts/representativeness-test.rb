#! /usr/bin/ruby -w

require 'representativeness'
require 'test/unit'

class RepresentativenessTest < Test::Unit::TestCase
    
    def test_other_hypos
        assert [:h_heads_only, :h_weighted], non_h(:h_fair)
        assert [:h_fair, :h_weighted], non_h(:h_heads_only)
    end
    
    def test_likelihood
        seq = 'HHHHH'
        assert_equal 0.5, likelihood('H', :h_fair)
        assert_equal 0.25, likelihood('HH', :h_fair)
        assert_equal 0.25, likelihood('HT', :h_fair)
        assert_equal 0.25, likelihood('TH', :h_fair)
        assert_equal 0.125, likelihood('THT', :h_fair)
        
        assert_equal 3.0/5, likelihood('H', :h_weighted)
        assert_equal 3.0/5 * 3.0/5, likelihood('HH', :h_weighted)
        assert_equal 3.0/5 * 2.0/5, likelihood('HT', :h_weighted)
        
        $all_hypothesis.each { |h|
            assert_equal likelihood('HTH', h), likelihood('HHT', h)    
        }
    end
    
    def test_representativeness 
        priors = {
            :h_fair => 0.6,
            :h_weighted => 0.3,
            :h_heads_only => 0.1
        }
        #assert_equal repr('HHHHH', :h_fair, priors), repr('TTTTT', :h_fair, priors)
        assert repr('HHHHH', :h_fair, priors) < repr('HHHTH', :h_fair, priors)
        
    end
    
end

