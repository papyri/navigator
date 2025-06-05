from saxonche import PySaxonProcessor
from thefuzz import fuzz
import xml.etree.ElementTree as ET
import argparse, os

def main(argv=None):
  parser = argparse.ArgumentParser()
  parser.add_argument('source', help='source directory')
  args = parser.parse_args()

  if not os.path.exists(args.source):
    print('Source directory does not exist')
    return
  
  processor = PySaxonProcessor(license=False)
  xsltproc = processor.new_xslt30_processor()
  xslt = xsltproc.compile_stylesheet(stylesheet_file='update-DDb.xsl')
  xslt2 = xsltproc.compile_stylesheet(stylesheet_file='finish-update.xsl')
  namespaces = {'tei': 'http://www.tei-c.org/ns/1.0'}

  source = os.path.abspath(args.source)
  if os.path.isdir(source):
    for root, dirs, files in os.walk(source):
      for file in files:
        if file.endswith('.xml'):
          xslt.set_property('s', os.path.join(root, file))
          xslt.set_property('o', os.path.join(root, file))
          xslt.set_parameter('id', processor.make_string_value(os.path.basename(file).replace('.xml', '')))
          xslt.transform_to_file()
          xml = ET.parse(os.path.join(root, file))
          target_refs = xml.findall('.//tei:body/tei:head/tei:ref[@target]', namespaces)
          plain_refs = xml.findall('.//tei:body/tei:head/tei:ref', namespaces)
          head = xml.find('.//tei:body/tei:head', namespaces)
          removed = []
          for ref in target_refs:
            ratio = 15
            found = None
            for plain in plain_refs:
              if plain.get('target', None) == None:
                if ratio < fuzz.token_set_ratio(ref.text, plain.text):
                  ratio = fuzz.token_set_ratio(ref.text, plain.text)
                  found = plain
            if found is not None:
              ref.text = found.text
              if not found.text in removed:
                head.remove(found)
                removed.append(found.text)
          xml.write(os.path.join(root, file), encoding='utf-8', xml_declaration=True)
          xslt2.set_property('s', os.path.join(root, file))
          xslt2.set_property('o', os.path.join(root, file))
          xslt2.transform_to_file()

if __name__ == '__main__':
  main()