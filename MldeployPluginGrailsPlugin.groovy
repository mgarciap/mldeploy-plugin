class MldeployPluginGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.4 > *"
    
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views",
            "grails-app/conf/mldeploy.groovy"
    ]

    def author = "Manuel Garcia"
    def authorEmail = "arquitectura@mercadolibre.com"
    def title = "New Word Grails deployment"
    def description = '''\\
Document me...
'''
    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/mldeploy-plugin"
}
