
[v1.1]
- added png editor
- added text editor
- added "create resource" option to create new text or image resources using the editors
- added search bar
- allowed filtering searches without reloading resources in between changes
- the explorer now wraps all individual resource-pack searches with exception catching and logging meaning searches should
almost never fail and will instead log the error and continue, skipping that resource-pack's assets. *(Often triggered by custom mod resource-pack classes, such as `crowdin-translate`'s CTResourcePack class)*