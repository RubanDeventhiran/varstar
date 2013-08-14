
$(document).ready(function(){
  setFileHandler();
  $('#interact').tabs(
//     { fx: { height: 'toggle', opacity: 'toggle' } }
  );
  setClearHandler();
  setDeployHandler();
  setQueryHandler();
  setPackageHandler();
  setLibraryHandler();
  setStatus("Awaiting files")
  
  setSidebar();
})
