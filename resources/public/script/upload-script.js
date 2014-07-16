var addToFileList = function(data) {
  $(data).each(function(index, item) {
    $('#fileList').append(
      $(document.createElement('div')).attr('class','fileListItem').append(
        $(document.createElement('input')).attr('type','checkbox')
        .attr('value',item),
        item)
    );
  });
};

var clearFileList = function() {
  $('#fileList').empty();
};

var setFileList = function(data) {
  clearFileList();
  addToFileList(data);
};

var getActiveFileList = function() {
  return $.grep($('.fileListItem').children('input'),function(val,index){
    return val.checked;
  }).map(function(val,index) {
    return val.value;
  });
};

var setFileHandler = function () {
  $('#input-action-upload').click(function(e) {
    e.preventDefault();
    var formData = new FormData(this);
    formData.append('file',$('#input-file-upload')[0].files[0]);
    $.ajax({
      url: '/upload',
      data: formData,
      cache: false,
      contentType: false,
      processData: false,
      type: 'POST',
      success: function(data){
        var jdata = $.parseJSON(data);
        setStatus(unescape(jdata.out));
        setFileList(jdata.data);
        $('#fileUpload')
        .not(':button, :submit, :reset, :hidden')
        .val('')
        .removeAttr('checked')
        .removeAttr('selected');
      }
    });
  });
}

var setClearHandler = function() {
  $('#input-action-clear').click(function(e) {
    $.post('/clear',function(callback){
      var jcallback = $.parseJSON(callback);
      setStatus(jcallback.out);
      clearFileList();
    });
  });
}

var setDeployHandler = function() {
  $('#input-action-deploy').click(function(e) {
    setStatus("Deploying...");
    var clear = $('#input-check-clear')[0].checked
    $.post('/deploy',
           {filter: getActiveFileList(),
            clear: clear},
           function(callback){
             var jcallback = $.parseJSON(callback);
             setStatus(jcallback.out);
             if (clear) {
               clearFileList();
             }
           });
  });
};
