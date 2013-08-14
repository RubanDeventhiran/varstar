var setSidebar = function(){
  $.get('/loaded',function(callback) {
  $('#sidebar-content').html(callback)})
  return true
}


var setStatus = function(inner) {
  $('#status').html(inner)
}