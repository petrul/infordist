
#
# measure hits on google
#

class GoogleHits

  # some ways to get google index size:
  #   http://www.google.com/search?hl=en&lr=&q=a+*+&btnG=Search
  #   http://www.google.com/search?hl=en&q=*-"a

    INDEX_SIZE = 20 * 1000 * 1000 * 1000
    #@index_size

    GOOGLE_SEARCH_API_KEY = "ABQIAAAAY8AGoDcxvqI7cqol-NomlBS_o4gTiJXlDY7tZtshl-ai_PIK7RSn2vWX4ujun3Fr67p9gv4jxM-4Rw"
    KEY_REGISTERED_FOR_SITE = "http://perso.telecom-paristech.fr/~dimulesc/"
    
#    DEBUG=false


    require 'rubygems'
    require 'json'
    require 'net/http'
    require 'cgi'
    
    def initialize(debug=false, lang=nil, site=nil)
        @debug = debug
        @default_options = {
            :rsz    =>  'small',
            :v      => '1.0',
            #:q => CGI.escape(term + " site:nytimes.com"),
            :key    => GOOGLE_SEARCH_API_KEY,
            #:meta => CGI.escape("lr=lang_fr"),
        }
        @baseurl = "http://ajax.googleapis.com/ajax/services/search/web?"

        @site = site
        if lang != nil
            @default_options[:meta] = CGI.escape("lr=" + lang)
        end

        @index_size = INDEX_SIZE # self.get_hits("a *")
        print_options(@default_options)
    end

    def get_hits(term)
        jsondata = call_google_search(term)
        jsonobj = parse_json(jsondata)
        return get_estimated_result_count(jsonobj).to_i
    end
    
    def get_size
      @index_size
    end

    ##########################################################################

    def print_options(opts)
        defopts = opts.dup
        defopts[:q] = 'test'
        defopts[:q] = 'test site:' + @site if @site != nil
        options_to_str = defopts.map { |key, value|  "#{key}=#{value}" }.join("&")
        STDERR.puts "** will use google search engine api: " + @baseurl + options_to_str
    end
    
    def call_google_search(term)
        options = @default_options.dup # copy defaults
        
        x_term = term
        x_term = term + " site:" + @site if @site != nil
        
        options[:q] =  CGI.escape(x_term)    

        options_to_str = options.map { |key, value|  "#{key}=#{value}" }.join("&")
        url = URI(@baseurl + options_to_str)
        
        STDERR.puts @baseurl + options_to_str if @debug
        Net::HTTP.get(url)
    end


    def parse_json(jsondata)
      JSON.parse(jsondata)
    end
    
    
    def get_estimated_result_count(jsonobj) 
        begin
            val = jsonobj['responseData']['cursor']['estimatedResultCount']
            raise RuntimeError.new("no results") if val == nil
            val
        rescue NoMethodError
            raise RuntimeError.new("no method error, something is wrong with json object " + jsonobj.to_s)
        end
    end

    private :call_google_search, :parse_json, :get_estimated_result_count, :print_options
    ####################################################################
    
end
