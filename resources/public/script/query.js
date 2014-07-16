var setQueryHandler = function () {
  $('#input-action-query').click(function(e) {
    $.post('/query',{query: $('#input-text-query')[0].value
					},function(callback){
      setStatus(callback)
      if (callback.indexOf('[Vertica][VJDBC]') == -1) {
        console.log($('#input-text-query'))
        $('#input-text-query')[0].value = ''
      }
    })
  })
  $('#input-text-query').keydown(function(e) {
    if (e.keyCode == 13) {
      e.preventDefault()
      $('#input-action-query').click()
    }
  })
}
