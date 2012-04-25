#! /usr/bin/ruby

#
# calculate normalized Google distance (NGD) following Cilibrasi & Vitanyi
#

def parse_cli
    require 'getoptlong'
    require 'rdoc/usage'
    
    opts = GetoptLong.new(
       [ '--help', '-h', GetoptLong::NO_ARGUMENT        ],
       [ "--quote-terms", '-q', GetoptLong::NO_ARGUMENT ]
       )
    
end


def main(argv)
  
  arg1 = argv[0]
  hits1 = get_estimated_result_count(parse_json(call_google_search("\"#{arg1}\"")))
  log_hits1 = log_base(2, hits1)
  
  argv[1..argv.length - 1].each { |arg2|

      #arg2 = argv[1]
      throw "please provide two terms to compare" if (arg1 == nil || arg2 == nil)
    
      puts "calculating NGD between '#{arg1}' and '#{arg2}' ..." if DEBUG
      
      #puts "response for #{arg1} : #{print_hash(parse_json(call_google_search(arg1)))}"
      hits2 = get_estimated_result_count(parse_json(call_google_search("\"#{arg2}\"")))
    
      the_two_args = "\"#{arg1}\" \"#{arg2}\""
      hits_together = get_estimated_result_count(parse_json(call_google_search(the_two_args)))
    
      log_hits2 = log_base(2, hits2)
      log_hits_together = log_base(2, hits_together)
    
      print_hits_info(arg1, hits1)
      print_hits_info(arg2, hits2)
      print_hits_info(the_two_args, hits_together)
    
      max_hits = max(log_hits1, log_hits2)
      min_hits = min(log_hits1, log_hits2)
    
      log_N = log_base(2, INDEX_SIZE)
    
      puts("(#{max_hits} - #{log_hits_together} ) / (#{log_N} - #{min_hits})") if DEBUG;
    
      ngd = (max_hits - log_hits_together ) / (log_N - min_hits)
    
      puts "NGD('#{arg1}', '#{arg2}') = #{ngd}"
  }
end

def print_hits_info(term, hits)
  g = hits.to_f() / INDEX_SIZE
  bigG = log_base(2, 1/g)
  log_hits = log_base(2,hits)

  puts "log(f('#{term}')) = log2(#{format_decimal(hits)}) = #{log_hits}; " +
    "g = f/N = #{g}; G = log(1/g) = #{bigG}" if DEBUG;
end

def log_base(n, x)
  return (Math.log(x) / Math.log(n))
end 

def max(x, y) 
  if x > y then x else y end 
end

def min(x, y) 
  if x < y then x else y end 
end

def format_decimal(st)
  st.to_s.gsub(/(\d)(?=(\d\d\d)+(?!\d))/, "\\1,")
end


main(ARGV)
