#! /usr/bin/ruby -w

# implementation of Griffiths and Tenenbaum, The Rational Basis of Representativeness, 2001


$all_hypothesis = [:h_fair, :h_heads_only, :h_weighted]

# belief degree in hypothesis
$prior_probabilities_example = {
	:h_fair => 0.2,
	:h_heads_only => 0.1,
    :h_weighted => 0.7 };

# the probabilities of hypothesis generating codes 
$hypothesis = {
    # fair coin
    :h_fair => proc { return 0.5 } ,
    # heads on the two faces
	:h_heads_only => proc { |symbol| if symbol == 'H' then 1 else 0 end },
    # biased coin
	:h_weighted => proc { |symbol| if symbol == 'H' then (3.0/5) else (2.0/5) end }
}


# likelihood of a data sequence under a hypothesis
def likelihood(sequence, hypo)
    hypo_code = $hypothesis[hypo]
    result = 1.0
    sequence.each_char { |it|
        result = result * hypo_code.call(it)
    }
    result
end

# a set of all other hypothesis
def non_h(hypothesis)
    result = []
    $all_hypothesis.each { |it| 
        result << it if it != hypothesis
    }
    result
end

# log [ P(d|h) / P(d|~h) ]
# where P(d|~h) = \sum_j P(d|h_j) * P(h_j|~h) 
# where P(h_j|~h) = P(hj) / (1 - P(h))
# @arg prior_probas: a hash : hypo x prior probability
def repr(d, h, prior_probas)
    other_hypos = non_h(h)
    # P(d|h)
    p_d_h = likelihood(d, h)
    
    # prior of h
    pp_h = prior_probas[h]
    
    # numitor : \sum_{h_j} P(d|h_j) P (h_j | non h_i)
    numitor = 0
    
    other_hypos.each { |hj|
        p_d_hj = likelihood(d, hj)
        pp_hj = prior_probas[hj]

        crt = p_d_hj * pp_hj / (1 - pp_h)
        numitor += crt
    }
    
    Math.log(p_d_h / numitor) / Math.log(2)
end

