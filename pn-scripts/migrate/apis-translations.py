from saxonche import PySaxonProcessor
import xml.etree.ElementTree as ET
import argparse, glob, os, re

def main(argv=None):
  parser = argparse.ArgumentParser()
  parser.add_argument('source', help='source directory')
  parser.add_argument('out', help='output directory')
  args = parser.parse_args()

  if not os.path.exists(args.source):
    print('Source directory does not exist')
    return
  
  if not os.path.exists(args.out):
    os.makedirs(args.out)
    print('Output directory created')
  
  processor = PySaxonProcessor(license=False)
  xsltproc = processor.new_xslt30_processor()
  xslt = xsltproc.compile_stylesheet(stylesheet_file='apis-translations.xsl')
  namespaces = {'tei': 'http://www.tei-c.org/ns/1.0'}

  source = os.path.abspath(args.source)
  if os.path.isdir(source):
    for root, dirs, files in os.walk(source):
      for file in files:
        if file.endswith('.xml'):
          xml = ET.parse(os.path.join(root, file))
          translation = xml.find('.//tei:body/tei:div[@type="translation"]', namespaces)
          HGV_ids = xml.findall('.//tei:publicationStmt/tei:idno[@type="HGV"]', namespaces)
          # Don't process if no translation or HGV id found
          if len(HGV_ids) == 0:
            continue
          if translation is None:
            continue
          for id in HGV_ids:
            for hgv in id.text.split(' '):
              dirname = re.sub(r'[a-z]', '', hgv)
              if len(dirname) < 4:
                dirname = dirname = '0'
              else:
                dirname = dirname[0:-3]
              os.makedirs(os.path.join(args.out, dirname), exist_ok=True)
              filename = os.path.join(args.out, dirname, f'{hgv}-1.xml')
              if os.path.exists(filename):
                filename = os.path.join(args.out, dirname, f'{hgv}-{str(len(glob.glob(os.path.join(args.out, str(dirname), hgv + "-*.xml"))) + 1)}.xml')
              xslt.set_property('s', os.path.join(root, file))
              xslt.set_property('o', filename)
              xslt.set_parameter('id', processor.make_string_value(os.path.basename(filename).replace('.xml', '')))
              xslt.transform_to_file()

if __name__ == '__main__':
  main()