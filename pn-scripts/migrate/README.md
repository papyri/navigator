# Migration Scripts

Creating the new structure involves `git move`ing DDbDP files to both the `DDbDP` (curated) and to the `static` directories. Because this is technically impossible, you need to follow these steps to execute the migration:

1. Create a `new_curated` (e.g.) branch and switch to it.
2. Run the `DDbDP-curated.sh` script in the `idp.data` repo root. Commit the changes.
3. Run `python3 updateddb.py </path/to/DDbDP>`. Commit the changes.
4. Run the `DCLP-curated.sh` script in the `idp.data` repo root. Commit the changes.
5. Run `python3 updatedclp.py </path/to/DCLP>`. Commit the changes.
6. Switch back to `master`; create a `new_static` (e.g.) branch and switch to it.
7. Run `DDbDP-static.sh` in the `idp.data` repo root. Commit the changes.
8. Switch back to `master`; create a `new_structure` (e.g.) branch and switch to it.
9. Run `git merge new_curated` then `git merge new_static`. There will be a merge conflict. Run `git commit -a` anyway and save. There aren't any actual conflicting files, just conflicting state, but Git will be able to cope with two files having the same extended (pre-move) history.
10. Run `translations.sh` and commit the result.

Dependencies: Assumes you have Saxon installed and available from the command line (the `translations.sh` script uses it). It's available via Homebrew.
