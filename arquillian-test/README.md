# Integration tests with Arquillian Cube on OpenShift

Arquillian Cube test project that creates a new temporary namespace with all the required containers in OpenShift. The test itself is being run outside this temporary namespace from the Jenkins' namespace (or even outside OpenShift). This was tested on Minishift with Jenkins deployed by the _jenkins-persistent_ template in the default _myproject_ namespace.

### Prepare run
Preparing the right permissions is required to run this test project.
* **image-puller**: The _app-users_ image should exist and be accessible from the temporary namespace. The _default_ serviceaccount should have _image-puller_ role, but we don't know the random name of the namespace in advance. Easiest is to grant permission to all service accounts from the build namespace _myproject_:

 `oc policy add-role-to-group system:image-puller system:serviceaccounts -n myproject`

* **serviceaccount:arquillian**: Create SA that will run the test:

 `oc create sa arquillian -n myproject`

 Grant permission to create new projects:

 `oc adm policy add-cluster-role-to-user self-provisioner system:serviceaccount:myproject:arquillian`

* **arquillian PodTemplate**: Copy the existing _maven_ PodTemplate in Jenkins Kubernetes plugin config. Use label _arquillian_ and set it to use service account _arquillian_. See _podtemplate-arquillian.xml_.

After these steps a Jenkins pipeline project can be created running the _Jenkins-arquillian_ pipeline (see parent directory).

### Steps:
These are the important steps of the Arquillian Cube test execution:
* Create temporary namespace with random name (e.g. 'arquillian-frp71'). Prefix is set in _namespace.prefix_ property.
* Run an optional script for preparation steps. In this test we need to add _view_ role for the _default_ sa in the temporary namespace to support using configMaps. See _env.setup.script.url_ property.
* Apply the yaml file to create resources in the temporary namespace. See _env.config.resource.name_ property.
* Populate components (database, mockserver) with initial data. See _ArquillianTest.before()_.
* Run tests
* Shutdown and delete the whole temporary namespace.

### Tests in ArquillianTest.class:
* **testSucc**: Happy path. Send a json message to _user.in_ and expecting the enriched json on _user.out_.
* **shutDownMariadb**: Tolerate temporary database issues. Delete _mariadb_ pod, send the test message then recreate _mariadb_ pod. The test message is automatically retried until the database is back online.
