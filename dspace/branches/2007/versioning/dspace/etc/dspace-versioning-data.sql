-- This is a small sql update to be run once that
-- will set all current items to revision 1 and indicate
-- that there is no previous revision.
UPDATE Item SET revision = 1, previous_revision = 0;