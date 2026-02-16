# Migration Scripts

Creating the new structure involves `git move`ing DDbDP files to both the `DDbDP` (curated) and to the `static` directories. Because this is technically impossible, you need to follow these steps to execute the migration:

## New Curated

1. Create a `new_curated` (e.g.) branch and switch to it.
2. Run the `DDbDP-curated.sh` script in the `idp.data` repo root. Commit the changes.
5. Run the `DCLP-curated.sh` script in the `idp.data` repo root. Commit the changes.
3. Run `python3 generate-current.py </path/to/idp.data>`. Git add the DDbDP folder to pick up the new files and commit the result.
4. Run `python3 updateddb.py </path/to/DDbDP>`. Commit the changes.
6. Run `python3 updatedclp.py </path/to/DCLP>`. Commit the changes.


## New Static

7. Switch back to `master`; create a `new_static` (e.g.) branch and switch to it.
8. Run `DDbDP-static.sh` in the `idp.data` repo root. Commit the changes.
9. Run `DCLP-static.sh` in the `idp.data` repo root. Run `git add Historical`. Commit the changes.
10. Run `python3 generate-historical.py </path/to/idp.data>`. Git add the DDbDP folder to pick up the new files and commit the result.
11. Run `python3 updatehistorical.py </path/to/Historical>`. Commit the changes.

## Merge

12. Switch back to `master`; create a `new_structure` (e.g.) branch and switch to it.
13. Run `git merge new_curated` then `git merge new_static`. There may be a merge conflict. Run `git commit -a` anyway and save. There aren't any actual conflicting files, just conflicting state, but Git will be able to cope with two files having the same extended (pre-move) history.
14. Run the `translations.sh` script in the `idp.data` repo root. Run `git add Translations` and commit the result.
15. Run `python3 apis-translations.py </path/to/APIS> </path/to/Translations>`. Run `git add Translations`. Commit the result.

Dependencies: Assumes you have Saxon installed and available from the command line (the `translations.sh` script uses it). It's available via Homebrew.
