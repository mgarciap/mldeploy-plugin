/**
* Mercadolibre's New World deployment configuration template
* You can configure depending on your needs.
*
* Some common configuration
* Servers Authentication Options
*  Credentials: 
*    Plain: ASCII username and password
*    Key  : Secure Shell Key file
*
*  Auth Modes: basic | credentialsFile | sshKey
*    basic: plain credentials provided in this configuration file (public repository)
*    credentialsFile: plain credentials provided in repository external file (local to deployer)
*    sshKey: encrypted authentication using SSH Key and paraphrase
*
*  Default Locations:
*    credentialFile: ${userHome}/credencials/${appName}/ssh.conf
*    sshKeyFile: ${userHome}/credencials/${appName}/${appName}-key
*      example: /home/carlitos/syi-api/syi-api-key
*/




// Environments
stage {
    // Check Local Vs Remote Repo sync
    // - true : there have to be no changes to pull or push
    // - false: onlylocal repository will be used for deployment tasks
    repositorySynchronization = false
    servers {
    	defaultWS {
    		description = "Default Staging Server"
        // Tomcat Plugin
        tomcat {
          useExternalCredentials = true
          contextPath="/defaultws" //leave blank to use app-name
        	username = "manager"
	      	password = "secret"	
	      	url =  "http://localhost:8080/manager"	
	      	//  externalCredentialsFile = "/absolute path to file"
        }
	      // Scripted
	      scripted {
			    host = "localhost"
			    // targetWarDir defaults to /deppartment/$app.name. Otherwise absolute path to dir
			    // targetWarDir = "/home/manuel/new-world/wars"
			    authenticationMode = "basic" //basic | credentialsFile | sshKey
          //  externalCredentialsFile = "/absolute path to file" 
          // sshKeyFile = "/absolute path to file" 
			    username = "manuel"
			    password = "AwqbGt65"
			    scripts {
			    	preDeploy = '${basedir}/scripts/predeploy.sh'
			    	postDeploy = '${basedir}/scripts/postdeploy.sh'
			    	rollback = '${basedir}/scripts/rollback.sh'
			    }
	      }
	    }
	  
      readOnlyWS {
    		description = "Read Only Staging Server"
        // Tomcat Plugin
        tomcat {
          useExternalCredentials = false
          contextPath="/read-only" //leave blank to use app-name
        	username = "manager"
	      	password = "secret"	
	      	url =  "http://localhost:8080/manager"	
	      	}
	      scripted {
			    host = "127.0.0.1"
			    authenticationMode = "credentialsFile" //basic | credentialsFile | sshKey
	     }
	   }  
	   
    writeOnlyWS {
    		description = "Write Only Staging Server"
    		tomcat {
          useExternalCredentials = false
          contextPath="/write-only" //leave blank to use app-name
        	username = "manager"
	      	password = "secret"	
	      	url =  "http://localhost:8080/manager"	
	      	}
 	      scripted {
			    host = "127.0.0.1"
			    authenticationMode = "sshKey" //basic | credentialsFile | sshKey
	      } 	
    }
  }
}

stage-special {
}

production {
	lalala = "lalala"
}

production-special {
}
