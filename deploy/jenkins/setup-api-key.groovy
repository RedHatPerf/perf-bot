import hudson.model.*
import jenkins.model.*
import jenkins.security.*
import jenkins.security.apitoken.*
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.CredentialsScope

// Inspired by https://github.com/jenkinsci/configuration-as-code-plugin/issues/1830

def token = '11c513a545425a50c202367deefad6ed33'
println("Setting up static Jenkins api key: " + token)

// script parameters
def userName = 'admin'
def tokenName = 'dev-token'

def user = User.get(userName)
user.getProperty(ApiTokenProperty.class).tokenStore.addFixedNewToken(tokenName, token)
user.save()

instance = Jenkins.instance
domain = Domain.global()
store = instance.getExtensionList(
        "com.cloudbees.plugins.credentials.SystemCredentialsProvider")[0].getStore()

usernameAndPassword = new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        tokenName,
        "DESCRIPTION",
        userName,
        token
)

store.addCredentials(domain, usernameAndPassword)