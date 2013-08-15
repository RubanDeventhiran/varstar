
$(document).ready(function(){
  setFileHandler();
  $('#interact').tabs();
  setClearHandler();
    setEnvWarnHandler();
  setDeployHandler();
  setQueryHandler();
  setPackageHandler();
  setLibraryHandler();
  setStatus("Awaiting files");
  
  setSidebar();
});
