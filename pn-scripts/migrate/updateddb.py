from saxonche import PySaxonProcessor
from thefuzz import fuzz
import xml.etree.ElementTree as ET
import argparse, csv, os, re

index = {}

def main(argv=None):
  parser = argparse.ArgumentParser()
  parser.add_argument('source', help='source directory')
  parser.add_argument('tmbase', help='TM base directory', default='/srv/data/papyri.info/TM')
  args = parser.parse_args()

  if not os.path.exists(args.source):
    print('Source directory does not exist')
    return
  
  processor = PySaxonProcessor(license=False)
  xsltproc = processor.new_xslt30_processor()
  xslt = xsltproc.compile_stylesheet(stylesheet_file='update-DDb.xsl')
  xslt2 = xsltproc.compile_stylesheet(stylesheet_file='finish-update.xsl')
  namespaces = {'tei': 'http://www.tei-c.org/ns/1.0'}
  
  with open('tm_checklist.csv', newline='') as tm:
    reader = csv.DictReader(tm, fieldnames=['DDb', 'Checklist', 'TM', 'replace', 'regex', 'notes'])
    for row in reader:
      if row['replace'] == 'TRUE':
        if index.get(row['TM'][0:4]) is None:
          index[row['TM'][0:4]] = [row]
        else:
          index[row['TM'][0:4]].append(row)

  source = os.path.abspath(os.path.join(args.source, 'DDbDP'))
  if os.path.isdir(source):
    for root, dirs, files in os.walk(source):
      for file in files:
        if file.endswith('.xml'):
          xslt.set_property('s', os.path.join(root, file))
          xslt.set_property('o', os.path.join(root, file))
          xslt.set_parameter('id', processor.make_string_value(os.path.basename(file).replace('.xml', '')))
          xslt.set_parameter('hgvbase', processor.make_string_value(args.source))
          xslt.set_parameter('tmbase', processor.make_string_value(args.tmbase))
          xslt.transform_to_file()
          xml = ET.parse(os.path.join(root, file))
          head = xml.getroot().find('.//tei:body/tei:head', namespaces)
          if head is None:
            print('Error: No head found in ' + os.path.join(root, file))
            continue
          target_refs = xml.findall('.//tei:body/tei:head/tei:ref[@target]', namespaces)
          for ref in target_refs:
            ratio = 15
            found = None
            for plain in xml.findall('.//tei:body/tei:head/tei:ref', namespaces):
              if plain.get('target') is None: # If there's no @target, but there is a <title>, this is a TM citation
                title = plain.find('tei:title', namespaces)
                if title is not None:
                  compare_text = get_replacement(title.text)
                else:
                  compare_text = plain.text
                if ratio < fuzz.token_sort_ratio(ref.text, compare_text):
                  ratio = fuzz.token_sort_ratio(ref.text, compare_text)
                  found = plain.get('n', None)
            if found is not None:
              found_ref = head.find(".//tei:ref[@n='" + found + "']", namespaces)
              found_ref.set('remove', 'true')
              children = found_ref.findall('*')
              if len(children) > 0:
                ref.text = ''
                for child in children:
                  if child.tag == '{http://www.tei-c.org/ns/1.0}title':
                    child.text = get_replacement(child.text)
                  ref.append(child)
                ref.set('n', found_ref.get('n', ''))
              else:
                ref.text = found_ref.text
          for rem in head.findall(".//tei:ref[@remove='true']", namespaces):
            try:
              head.remove(rem)
            except Exception:
              print('Could not remove node' + rem.get('n', '') + ' from ' + os.path.join(root, file))
          xml.write(os.path.join(root, file), encoding='utf-8', xml_declaration=True)
          xslt2.set_property('s', os.path.join(root, file))
          xslt2.set_property('o', os.path.join(root, file))
          xslt2.transform_to_file()

def get_replacement(title):
  key = title[0:4]
  if index.get(key) is not None:
    for item in index[key]:
      if item['regex'] == 'TRUE':
        found = re.search(item['TM'], title)
        if found:
          if len(found.groups()) > 0:
            return re.sub('$1', found.group(1), item['Checklist']) + title[found.end():]
          else:
            return item['Checklist'] + title[found.end():]
      elif title.startswith(item['TM']):
        return item['Checklist'] + title[len(item['TM']):]
  return title

if __name__ == '__main__':
  main()