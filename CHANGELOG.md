
[v1.3]
- updated to 1.21.3
- added more concurrent export preventions as well as display messages to inform users of an export in progress when attempting to export again

[v1.2]
- fixed forge 1.21
- fixed a CME crash when exporting folders, also prevented exporting new files and folders while a previous large export is still running
- fixed not being able to click the file list scrollbar
- changed the description of the export folder
- restructured the explorer start screen to begin a level above the `assets/` folder rather than inside it, allows exporting every asset easily with 1 click
- added clarification text to the fabric-api generic folder, it holds all the fabric-api namespaces to prevent cluttering the namespace directory

[v1.1]
- added png editor
- added text editor
- added "create resource" option to create new text or image resources using the editors
- added search bar
- allowed filtering searches without reloading resources in between changes
- the explorer now wraps all individual resource-pack searches with exception catching and logging meaning searches should
almost never fail and will instead log the error and continue, skipping that resource-pack's assets. *(Often triggered by custom mod resource-pack classes, such as `crowdin-translate`'s CTResourcePack class)*