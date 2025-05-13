import hudson.model.*
import jenkins.model.*
import jenkins.security.*
import jenkins.security.apitoken.*
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.CredentialsScope

println("Setting up Jenkins api key")

// script parameters
def userName = 'admin'
def tokenName = 'test-token'

def user = User.get(userName)
def apiTokenProperty = user.getProperty(ApiTokenProperty.class)
def result = apiTokenProperty.tokenStore.generateNewToken(tokenName)
user.save()

// so that we can use it
println("Generated Jenkins api key: " + result.plainValue)

instance = Jenkins.instance
domain = Domain.global()
store = instance.getExtensionList(
        "com.cloudbees.plugins.credentials.SystemCredentialsProvider")[0].getStore()

usernameAndPassword = new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        "TOKEN_NAME",
        "DESCRIPTION",
        userName,
        result.plainValue
)

store.addCredentials(domain, usernameAndPassword)