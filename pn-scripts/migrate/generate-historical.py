from saxonche import PySaxonProcessor
import xml.etree.ElementTree as ET
import argparse, os

index = {}

def main(argv=None):
  parser = argparse.ArgumentParser()
  parser.add_argument('source', help='source directory')
  args = parser.parse_args()

  if not os.path.exists(args.source):
    print('Source directory does not exist')
    return
  
  processor = PySaxonProcessor(license=False)
  xsltproc = processor.new_xslt30_processor()
  xslt = xsltproc.compile_stylesheet(stylesheet_file='../../pn-xslt/GenerateHistorical.xsl')

  source = os.path.abspath(os.path.join(args.source, 'HGV_meta_EpiDoc'))
  print('Processing source directory: ' + source)
  if os.path.isdir(source):
    for root, dirs, files in os.walk(source):
      for file in files:
        if file.endswith('.xml'):
          xslt.set_property('s', os.path.join(root, file))
          xslt.set_parameter('base', processor.make_string_value(args.source))
          xslt.transform_to_string()

if __name__ == '__main__':
  main()