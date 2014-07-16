$(document).ready(function(){
  setFileHandler();
  $('#interact').tabs();
  setClearHandler();
  setDeployHandler();
  setQueryHandler();
  setPackageHandler();
  setLibraryHandler();
  setStatus("Awaiting files");

  setSidebar();
});
