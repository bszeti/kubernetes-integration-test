#This script is run after the namespace is created/chosen (see $KUBERNETES_NAMESPACE), but before applying kubernetes.yaml.
#It's run under the Arquillian test process, so 'oc' should be available on the test host as it's used in this script.

#The best practice is to have 'oc' on the test host logged in with a user (e.g. system:serviceaccount:cicd:arquillian)
# and don't overwrite in arquillian.xml, so this script and Arquillian uses the same oc user
#Make sure that this user can create new projects:
# oc adm policy add-cluster-role-to-user self-provisioner system:serviceaccount:cicd:arquillian

#Check where we are
pwd
env

#oc is independent from arquillian.xml, so the "current" logged in user is used (e.g. service account of the pod)
#the current 'oc project' is also NOT the namespace created by Arquillian
oc whoami
oc project


#Make sure the 'default' SA in our $KUBERNETES_NAMESPACE can pull image from cicd project
#Easiest is to allow any serviceaccount to pull images from the ci/cd project in advance
# oc policy add-role-to-group system:image-puller system:serviceaccounts -n cicd
#Or if the current user has admin permissions, you can add image-puller role for the new namespace's default SA
# oc policy add-role-to-user system:image-puller system:serviceaccount:$KUBERNETES_NAMESPACE:default -n cicd


#Allow 'default' SA to read configMap, actually this is why we have this script in this example. This is not needed if you don't want to read configMaps in your pods.
#Make sure that the current 'oc whoami' has the right permissions. If Arquillian created the project, the current user can run this
oc policy add-role-to-user view system:serviceaccount:$KUBERNETES_NAMESPACE:default -n $KUBERNETES_NAMESPACE
