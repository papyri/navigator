from saxonche import PySaxonProcessor
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
  xslt = xsltproc.compile_stylesheet(stylesheet_file='fix-historical.xsl')

  source = os.path.abspath(args.source)
  if os.path.isdir(source):
    for root, dirs, files in os.walk(source):
      for file in files:
        if file.endswith('.xml'):
          xslt.set_property('s', os.path.join(root, file))
          xslt.set_property('o', os.path.join(root, file))
          xslt.transform_to_file()

if __name__ == '__main__':
  main()