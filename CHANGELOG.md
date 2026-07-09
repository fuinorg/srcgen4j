# Change Log
Release notes for srcgen4j.

### [0.5.0]
- Uses **Java 17**
- Switched to `jakarta` namespace
- Use fuin.org BOM
- Added [Replacer](commons/src/main/java/org/fuin/srcgen4j/commons/Replacer.java)
- Simplified getting module and folder for generated artifacts 
- Renamed type `Project` to `Module`
  - the config elements `<projects>`/`<project>` are now `<modules>`/`<module>`
  - the `project` attribute of `<artifact-factory>` and `<target-file>` is now `module`

### [0.4.2]
- Uses **Java 8**
