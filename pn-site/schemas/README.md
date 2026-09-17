# Papyri.info Schemas

There is a complex toolchain at work to permit both Schematron and RelaxNG validation via the EpiDocinator.
PN documents edited in [oXygen](https://www.oxygenxml.com/) can use a RelaxNG schema that combines RelaxNG rules and Schematron rules, and so does both kinds of validation at once. They will use `xml-model` schema references like:

```xml
<?xml-model href="https://papyri.info/schemas/papyri.info.rng" type="application/xml" schematypens="http://relaxng.org/ns/structure/1.0"?>
<?xml-model href="https://papyri.info/schemas/papyri.info.rng" type="application/xml" schematypens="http://purl.oclc.org/dsdl/schematron"?>
```

Note that both point to the same schema, but use different schema types. This will be useful for people editing documents in a desktop environment, allowing us to provide more fine-grained validation than has been the case before.

The RelaxNG schema is built from a custom TEI ODD, `papyri.info_odd.xml`, itself based on a copy of the EpiDoc ODD, `tei-epidoc-9.7.compiled.xml`, which is in turn based on TEI 4.10.2. Note the "compiled" in the filename. ODDs can be "chained" meaning that the EpiDoc ODD is built from the full TEI ODD with the EpiDoc customizations. The ODD compilation process takes the customization instructions and, optionally, a (compiled) source ODD, and uses the customization instructions to build a compiled ODD with the relevant components.

The TEI Stylesheets provide scripts for building compiled ODDs and RelaxNG schemas from source ODDs:

```sh
/path/to/TEI/Stylesheets/bin/teitorng papyri.info_odd.xml papyri.info.rng
```

Will make a RelaxNG that includes the Schematron constraints.

Making Schematron work in the EpiDocinator environment is nowhere near as simple, unfortunately. Schematron can be made to function as an XSLT that produces a report in an XML language call SVRL (Schematron Validation Reporting Language), but it has to be extracted as standalone Schematron first:

```sh
saxon -xsl:../../pn-xslt/extract-isosch.xsl -s:papyri.info.rng -o:papyri.info.isosch.sch
```

[Saxon](https://www.saxonica.com/welcome/welcome.xml) is an XSLT processor which can be installed using Homebrew, or downloaded from the website. The free "Home Edition" (HE) will do what we need it to. Once you've extracted the Schematron file, it can be "transpiled" into XSLT:

```sh
saxon -xsl:../../pn-xslt/schxslt/src/main/resources/content/transpile.xsl -s:papyri.info.isosch.sch -o:../../pn-xslt/papyri.info.schematron.xsl schxslt:compact-report=true schxslt:report-fired-rule=false schxslt:report-active-pattern=false
```

(this assumes you are running the transpile step from inside the [`schemas/`](https://gitlab.oit.duke.edu/papyri/navigator/-/tree/master/pn-site/schemas?ref_type=heads) directory)

Now you have an XSLT that the EpiDocinator can use to process files with the reference <https://papyri.info/schemas/papyri.info.rng>. The schemas and XSLTs are copied into it from the latest Navigator repo when the EpiDocinator image is built.
