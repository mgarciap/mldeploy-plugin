/**
*  Mercadolibre's New World Deployment Scripts
*    - Stage Deploys (several flavor)
*    - Production Deploys (several flavor)
*
*/

includeTargets << grailsScript("Init")

scriptEnv = Ant.project.properties.'grails.env'

includeTargets << new File("$mldeployPluginPluginDir/scripts/_MLTomcatDeploy.groovy")
includeTargets << new File("$mldeployPluginPluginDir/scripts/_MLScriptedDeploy.groovy")

//TODO: poder deployar una version especifica. Requeriría hacer un git checkout 'tag'
target(main:"Provides Ground-Zero Deployment utilities") {
  depends(parseArguments)
  targetLabel = '[DEBUG - New World Deploy]'
  println ""
  println "***********************************************************"  
  println "*      Mercadolibre New World Deployment utilities        *"
  println "***********************************************************"
  println ""

  def params = argsMap.params
  println "$targetLabel Params: $argsMap.params"
  def mode  = params ? params[0]?.trim() : null
  println "$targetLabel Deployment Mode: '$mode'" + (mode == null ? '(defaulting to deploy)':'')
  if(mode != null && 
     (mode.equals('tomcat') ||
      mode.equals('scripted')) &&
     params.size() > 1) {
     argsMap.params = params[1..-1]
  }  
  
  def action = params ? params[0]?.trim() : null
  
  if(action != null && 
     (action.equals('deploy') ||
      action.equals('undeploy') ||
      action.equals('list')) &&
     params.size() > 1) {
     argsMap.params = params[1..-1]
  }  
  
  println "$targetLabel Target Action '$mode'" + (mode == null ? '(defaulting to deploy)':'')
  println "$targetLabel	Custom Environment: $scriptEnv"
  
  switch(mode) {
    case 'help': // calls Tomcat Plugin targets
        printHelp()
    break
    
    case 'scripted': 
    		switch(action) {
		  		case 'help':  
		        scriptedPrintHelp()
			  	break
			  	
			  	case 'undeploy':  
		      	scriptedUndeployApplication()
			  	break
			  	
			  	case 'rollback':  
		      	scriptedRollbackApplication()
			  	break
			  	
		      case 'predeploy':  
		      	scriptedPredeployScript()
			  	break
			  	
			  	case 'postdeploy':  
		      	scriptedPostdeployScript()
			  	break	    	
			  	
			  	default:
		      	scriptedDeployApplication()
			  	}
    break
    
    default:
    		switch(action) {
    			case 'help':  
		        tomcatPrintHelp()
			  	break
			  	
			  	case 'undeploy':  
		      	tomcatUndeployApplication()
			  	break
			  	
			  	case 'list':  
		      	tomcatListApplications()
			  	break
			  	
			  	default:
        		tomcatDeployApplication()
	    	}	    	    
  }  
}

target(printHelp:"How to use this plugin") {
  println """
 **********************************
* New World Deployment Plugin Usage*
 **********************************
> grails MLdeploy [MODE] [ACTION] [server]
----------------------------------------
MODE,
  tomcat: grails tomcat plugin
  scripted: ML custom scripts
  defaults: tomcat
	
ACTION,
  deploy
  undeploy
  list
  defaults: deploy
  
server,
   target server
   defaults: ALL environment servers
   
Examples:
  grails mldeploy tomcat deploy ws-front-end
  ---> Deploys via Tomcat Plugin to specified server.
  """
}

setDefaultTarget("main")

