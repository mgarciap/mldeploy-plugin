/**
*  Mercadolibre's New World Deployment Scripts: Release
*    - Only GIT
*
*/


includeTargets << grailsScript("Init")

scriptEnv = Ant.project.properties.'grails.env'

includeTargets << new File("${basedir}/scripts/_MLVersioning.groovy")

target(main:"Provides Ground-Zero Deployment utilities") {
  depends(parseArguments)
  targetLabel = '[DEBUG - New World Versioning]'
  println ""
  println "***********************************************************"  
  println "*      Mercadolibre New World Versioning utilities        *"
  println "***********************************************************"
  println ""

  def params = argsMap.params
  println "$targetLabel Params: $argsMap.params"
  def action  = params ? params[0]?.trim() : null
  if(params.size() > 1) {
    argsMap.params = params[1..-1]
  } else {
    argsMap.params.clear()
  }
  println "$targetLabel	Custom Environment: $scriptEnv"
  println "$targetLabel Target Action '$action'" + (action == null? '(defaulting to release)':'')
  
    		switch(action) {
    			case 'tag-build':  
    			  tagBuild()
			  	break
			  	
			  	case 'action2':  
            println "Action 2"
			  	break
			  	
          default:
            release()
	    	}	    	
  }  

/**
* Call from bamboo CI
* -DbambooBuildNumber=${bamboo.buildNumber} must be passed
* as System Environment Variables
*/
target(tagBuild: 'To be used from Bamboo CI') {
  targetLabel = '[DEBUG - Tag Build]'
  println "$targetLabel Parametes left (should be buildNumber): $argsMap.params"
  tagAndCommitTag()
 }


/**
* Release current version and upgrede to a new version
*/
target(release: 'To be used from Bamboo CI') {
  targetLabel = '[DEBUG - Release]'
  println "$targetLabel Starting releasing . . ."
  checkRepositorySynchronizationStatus()
	tagAndCommitTag()
	upgradeVersion() 
	commitUpgradedVersion()
  println "$targetLabel Release finished"   
 }


target(printHelp:"How to use this plugin") {
  println """
 *****************************      
* New World Versioning Plugin *
 *****************************
> grails mlversion []ACTION] [version-level]
----------------------------------------
ACTION,
  tag-build
  defaults to release
  
  //////??????
version-level,
  Increases indicated sequence version and updates apps metadata:
    none: overwrites current tag
    major: x+1.y.z
    minor: x.y+1.z
    internal: increase internal version (alpha, beta, release candidate) x.y.z+1

server,
   Target server (defaults to ALL environment servers)
   
Examples:
  grails MLDeploy tomcat deploy minor ws-front-end
  Deploys via Tomcat Plugin to specified server. Tags version and increase ir as minor release.
  """
}


/*
Code references and examples:
  // Retreave build number from JVM parameter: -DbambooBuildNumber=xxx
//  buildNumber = Ant.project.properties.'bambooBuildNumber'  
//  println "$targetLabel Tag build number suffix: $buildNumber"
*/

setDefaultTarget("main")

