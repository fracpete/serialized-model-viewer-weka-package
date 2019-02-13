# serialized-model-viewer-weka-package

Adds a tab to the Weka Explorer which allows the user to load a serialized
model (or actually any Java serialized file) and display the string
representation of its content.

It displays:
* the model 
* the model graph/tree if it implements `weka.core.Drawable` (e.g., J48, REPTree or BayesNet)
* the header of the training data (if present)


## Releases

* [2019.2.14](https://github.com/fracpete/serialized-model-viewer-weka-package/releases/download/v2019.2.14/serialized-model-viewer-2019.2.14.zip)
* [2019.1.27](https://github.com/fracpete/serialized-model-viewer-weka-package/releases/download/v2019.1.27/serialized-model-viewer-2019.1.27.zip)
* [2015.3.26](https://github.com/fracpete/serialized-model-viewer-weka-package/releases/download/v2015.3.26/serialized-model-viewer-2015.3.26.zip)


## How to use packages

For more information on how to install the package, see:

https://waikato.github.io/weka-wiki/packages/manager/

## Screenshot

Displaying a `REPTree` model:

![REPTree model](src/site/resources/reptree_model.png)

Popup menu for copying/saving the textual output (in this case, the graph):

![REPTree popup menu](src/site/resources/popup_menu.png)

BayesNet graph in XML BIF format:

![BayesNet graph](src/site/resources/bayesnet_graph.png)
