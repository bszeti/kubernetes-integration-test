#This script is run after the namespace is created/chosen (in $KUBERNETES_NAMESPACE), but before applying kubernetes.yaml.
#It's run under the process running the Arquillian test app, so 'oc' should be available, it uses the current 'oc whoami' user and project (not $KUBERNETES_NAMESPACE)
#Permission for the current 'oc whoami' user must be ok to run the script

#Check where we are
pwd
oc project
env

#Make sure the 'default' sa in our $KUBERNETES_NAMESPACE can pull image from cicd project
oc policy add-role-to-user system:image-puller system:serviceaccount:$KUBERNETES_NAMESPACE:default -n myproject

#Allow 'default' sa to read configMap
oc policy add-role-to-user view system:serviceaccount:$KUBERNETES_NAMESPACE:default -n $KUBERNETES_NAMESPACE
