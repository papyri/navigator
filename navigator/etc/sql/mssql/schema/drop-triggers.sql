IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='TR' AND name='trig_folder')
    DROP TRIGGER trig_folder;

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='TR' AND name='trig_folder_menu')
    DROP TRIGGER trig_folder_menu;

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='TR' AND name='trig_page_menu')
    DROP TRIGGER trig_page_menu;

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='TR' AND name='trig_fragment')
    DROP TRIGGER trig_fragment;

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='TR' AND name='trig_security_principal')
    DROP TRIGGER trig_security_principal;

