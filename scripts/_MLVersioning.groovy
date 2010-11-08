/**
*  Mercadolibre's New World Versioning Scripts
*  (Only GIT SCM)
*    - default: increment app internal version by 1
*    - minor
*    - major
*
*  - The current appVersion is the tag that has not been deployed. 
*    When it is deployed, the version is upgraded depending on the version scheme specified
*
*  Versionning Scheme (http://en.wikipedia.org/wiki/Software_versioning#Sequence-based_identifiers)
*  Example: app.name-major.minor.release_type
*  Major number is increased when there are significant jumps in functionality. 
*  Minor number is incremented when only minor features or significant fixes have been added.
*  Release type is an alphanumeric string denoting the release type. i.e. "alpha", "beta" or "release candidate". 
*  A release train using this approach might look like 0.5, 0.6, 0.7, 0.8, 0.9 == 1.0b1, 1.0b2 (with some fixes),
*  1.0b3 (with more fixes) == 1.0rc1 (which, if it is stable enough) == 1.0. If 1.0rc1 turns out to have 
*  bugs which must be fixed, it turns into 1.0rc2, and so on.
*/


/**
* It's highly recommended to run every kind of local tests before tagging
* include any necessary targets below
*/
includeTargets << grailsScript("Init")

/*
* MG: 'Tag version following the versionning scheme.'
* Takes the version parameter and increments the version depending on it. 
* If none is specified, the internal release is incremented (i.e.: x.y.(z+1))
*/
target(upgradeVersion: 'Upgrade application version to next version') {
  def targetLabel = "[DEBUG - Upgrade Version]"
	def appVersion = metadata.'app.version'
	def appName = metadata.'app.name'
	println "$targetLabel Aplication $appName. Current Version $appVersion"

  def params = argsMap.params
  def targetVersionType = params ? params[0]?.trim() : null

/*
*  In case 'next' is especified
*/
	currentVersion = appVersion.tokenize('.')
	// Initialize version scheme to major.minor.releaseType
	for ( i in 0..2 ) {
    currentVersion[i] = currentVersion[i]? currentVersion[i] : '0'
  }
  
  println "$targetLabel Increasing sequence: " + (targetVersionType != null ? targetVersionType : "not specified (Internal by default)")
  
  // If version parameter was provided, lets 'eat' it from arguments
  if(targetVersionType != null && 
     (targetVersionType.equals('major') ||
      targetVersionType.equals('minor') ||
      targetVersionType.equals('internal')) &&
     params.size() > 1) {
     argsMap.params = params[1..-1]
  }
  
  def versionIndex = null
  switch(targetVersionType) {
      case 'major':
        versionIndex = 0
        break        
      case 'minor': 
        versionIndex = 1
        break
      case 'internal':  //releaseType
        versionIndex = 2
      break  
      default: //Overwrite current TAG
        versionIndex = 2      
        break
  }  
  if (versionIndex != null) {
    currentVersion[versionIndex] = currentVersion[versionIndex]? (currentVersion[versionIndex] as int)+ 1 : '1'
  }

  def newVersion = currentVersion.join('.')
  println "$targetLabel New Application Version: $newVersion"

	if (!metadata.'app.version'.equals(newVersion)) {
		metadata.'app.version' = newVersion
		metadata.persist()
	}
	// TODO: Refactorizar para cuando se transforme en plugin
	//event("StatusFinal", [ "Application version updated to $newVersion"])
	println "$targetLabel application.properties updated: app.version=$newVersion"
}


/*
* Is the local repository holding changes that haven't been pushed to remote?
*/
target(checkRepositorySynchronizationStatus: 'Verify that there is no diferences between local and remote repositories') {
  def targetLabel = "[Check Repo Sync Status]"
  def config = new ConfigSlurper().parse(new File("${basedir}/grails-app/conf/mldeploy.groovy").toURL())
  if (!config.stage.repositorySynchronization){
    println "$targetLabel Repository Synchronization Status Check: DISABLED!"
  	return
  }
  
  println "$targetLabel Checking working directory synchronization status ..."
	exec  resultproperty:'cmdreturn1', outputproperty:'cmdout1', failonerror:true, executable:'git', { arg line:'status --short'}
	if (ant.project.properties.cmdout1?.equals("")) {
		println "$targetLabel Working directory Synchronized!"
	} else {
	  println "$targetLabel Working directory out of Sync: You need to add and/or commit to your Local Repo"
	  exit(1)
	}
	  
	println "$targetLabel Checking Server Vs Local repository synchronization status ..."
	exec resultproperty:'cmdreturn2', outputproperty:'cmdout2', failonerror:true, executable:'git', { arg line:'remote show origin'}
	if (ant.project.properties.cmdout2.contains("local out of date")) {
		println "$targetLabel Remote repository changes not pulled yet. Pull needed!" 
		println "$targetLabel (You'll probably need to re-run your tests)"
		exit(1)
	} else if (ant.project.properties.cmdout2.contains("fast-forwardable")) {
	  println "$targetLabel Local repository changes not pushed yet. Push needed!"
	  exit(1)
	  } else {
	    println "$targetLabel Local and Remote repositories synchronized!"
	    }	  
}

target(tagAndCommitTag: 'Tag this app.version and commit it. Only this tag') {
  def targetLabel = "[DEBUG - Tag and commit TAG]"
	
  def suffix  = argsMap.params ? argsMap.params[0]?.trim() : null
  argsMap.params.clear()
  
 	tag = metadata.'app.version'
  println "$targetLabel Current App Version = $tag"
  if (suffix != null && !suffix.isEmpty()) {
    println "$targetLabel Applying Build Number suffix: $suffix"
   	tag += '.' + suffix
  } else {
    println "$targetLabel No Build Number received. App Version metadata will be used as tag: $tag"
  }
  
  println "$targetLabel Creating Tag: $tag"
  
	exec  resultproperty:'cmdreturn_tagAndCommitTag', outputproperty:'cmdout_tagAndCommitTag', failonerror:true, executable:'git', { arg line:'tag'}	     
	if (ant.project.properties.cmdout_tagAndCommitTag.contains("$tag")) {
		println ""
		println "$targetLabel There is already a Tag nemed: $tag. Tag will be overwritten(forced)!!!"
		println ""
	}
	// If there ara changes since last tag, the tag will be overwrited anyways
	exec executable:"git", {arg line:"tag --force $tag"}
	println "$targetLabel Tag created: $tag" 
	
	println "$targetLabel Pushing tag: $tag" 
	exec executable:"git", {arg line:"push origin $tag"}
	println "$targetLabel Tag pusshed: $tag"  
}

target(commitUpgradedVersion: 'Commit the Upgraded Version (appVersion) metadata') {
	def appVersion = metadata.'app.version'
	exec executable:"git", {
		arg line:"commit application.properties -m 'Version Upgraded my ML Deployment plugin [$appVersion]'"
	}
}


/*
* MG: Check consistency between current application metadata (application.properties) and 
* repository tags
*/
target(checkExistingCurrentVersionTag: 'Upgrade application version to next version') {
  def targetLabel = "[Tag validation]"
	def appVersion = metadata.'app.version'
	println "$targetLabel Checking current version existing Tag ..."
	exec  resultproperty:'cmdreturn_cecvt', outputproperty:'cmdout_cecvt', failonerror:true, executable:'git', { arg line:'tag'}	     
	if (!ant.project.properties.cmdout_cecvt.contains("$appVersion")) {
		println "$targetLabel There is no Tag for current version: $appVersion. Tag one!"
	  exit 1
	} else {
	  println "$targetLabel There is a Tag for current version: $appVersion"
	}
	
}

setDefaultTarget(upgradeVersion)
