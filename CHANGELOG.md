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
- Added [SrcGen4JConfigValidator](commons/src/main/java/org/fuin/srcgen4j/commons/SrcGen4JConfigValidator.java)
  - a `srcgen4j-config.xml` is validated against the XML schema before parsing and generation start
  - an invalid file fails the build listing every problem with its line and column
  - artifacts contribute their XSDs via a `META-INF/srcgen4j/schemas` file (see [SrcGen4JSchemas](commons/src/main/java/org/fuin/srcgen4j/commons/SrcGen4JSchemas.java))

### [0.4.2]
- Uses **Java 8**
