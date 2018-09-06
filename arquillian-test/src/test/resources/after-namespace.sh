#This script is run after the namespace is created/chosen (in $KUBERNETES_NAMESPACE), but before applying kubernetes.yaml.
#It's run under the process running the Arquillian test app, so 'oc' should be available, it uses the current 'oc whoami' user and project (not $KUBERNETES_NAMESPACE)
#Permission for the current 'oc whoami' user must be ok to run the script
#Let's assume the test is run by system:serviceaccount:myproject:arquillian and it has "admin" role

#Check where we are
pwd
oc project $KUBERNETES_NAMESPACE
env
oc whoami


#Make sure the 'default' sa in our $KUBERNETES_NAMESPACE can pull image from cicd project
#Easiest is to allow any serviceaccount to pull images from the ci/cd project in advance
# oc policy add-role-to-group system:image-puller system:serviceaccounts -n myproject
#This requires admin privileges for the account running the test
#oc policy add-role-to-user system:image-puller system:serviceaccount:$KUBERNETES_NAMESPACE:default -n myproject


#Allow 'default' sa to read configMap
#Requires permission sot arquillian sa
#oc adm policy add-cluster-role-to-user self-provisioner system:serviceaccount:myproject:arquillian
oc policy add-role-to-user view system:serviceaccount:$KUBERNETES_NAMESPACE:default -n $KUBERNETES_NAMESPACE
