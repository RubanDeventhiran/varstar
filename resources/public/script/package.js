var setPackageHandler = function() {
  $('#input-action-package').click(function(e) {

    var formData = new FormData(this);
    formData.append('file',$('#input-file-package')[0].files[0])
    $.ajax({
      url: '/package',
      data: formData,
      cache: false,
      contentType: false,
      processData: false,
      type: 'POST',
      success: function(callback){
        var jcallback = $.parseJSON(callback);
        setStatus(jcallback.out);
        $('#input-file-package')
        .not(':button, :submit, :reset, :hidden')
        .val('')
        .removeAttr('checked')
        .removeAttr('selected');
      }
    });
  })
}

var setLibraryHandler = function () {
  $('#input-action-library').click(function(e) {
    $.post('/install',
           {package: $('#input-text-library')[0].value},
           function(callback){
             var jcallback = $.parseJSON(callback);
             setStatus(jcallback.out)
             $('#input-text-library')[0].value=''
           })
  })
  $('#input-text-library').keydown(function(e) {
    if (e.keyCode == 13) {
      e.preventDefault()
      $('#input-action-library').click()
    }
  })}
