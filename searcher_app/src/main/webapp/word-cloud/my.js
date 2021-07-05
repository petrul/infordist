function getParam(paramName) {
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++)
    {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == paramName)
        {
            return sParameterName[1];
        }
    }
}

function getNeighbours() {
    term = getParam('term')
    if (!term) {
        term = 'kingdom' // default
    }
    console.log('getting neighbours for', term)

    const url = `https://scriptorium.hopto.org/searcher_app/api/endpoint?term=${term}&size=100`
    console.log(url)
    $.getJSON(url, function( data ) {
        console.log('got', data)
        var items = [];
        $.each( data, function( key, val ) {
          items.push( "<li id='" + key + "'>" + val + "</li>" );
        });
       
        $( "<ul/>", {
          "class": "my-new-list",
          html: items.join( "" )
        }).appendTo( "body" );
      });
}

getNeighbours()