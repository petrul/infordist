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

async function getNeighbours() {

    term = getParam('term')
    if (!term) {
        term = 'kingdom' // default
    }
    console.log('getting neighbours for', term)

    const url = `https://scriptorium.hopto.org/searcher_app/api/endpoint?term=${term}&size=100`
    console.log(url)

    result = [];

    let ajaxPromise = new Promise( (res, rej) => {
        $.ajax({
            url: url,
            type: 'GET',
            crossDomain: true,
            success: data => res(data),
            error: err => rej(err)
        });
    });

    const data = await ajaxPromise
    
    // console.log('data', data);
    result = data.map(it => {
        return {
            text: it.text,
            weight: 1.0 / it.dist,
            link: 'index.html?term=' + it.text,
            html: {
                title: `Click for ${it.text}'s semantic neighbourhood`, 
                class: "custom-class"
            }
        }
    })
    // console.log('xformed data' , result)
    return result;
}

// getNeighbours().then(data => {
//     console.log('data', data);
// })

