DROP TABLE IF EXISTS `voice`.`calls`;
CREATE TABLE  `voice`.`calls` (
  `CallId` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `FirstUser` int(10) unsigned NOT NULL,
  `SecondUser` int(10) unsigned NOT NULL,
  `CallSession` varchar(150) NOT NULL,
  `FirstSdp` varchar(1000) DEFAULT NULL,
  `SecondSdp` varchar(1000) DEFAULT NULL,
  `Status` int(10) unsigned DEFAULT '0',
  PRIMARY KEY (`CallId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `voice`.`requests`;
CREATE TABLE  `voice`.`requests` (
  `Id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `SenderId` int(10) unsigned NOT NULL,
  `ReceiverId` int(10) unsigned NOT NULL,
  `Text` varchar(400) NOT NULL,
  `Status` varchar(45) NOT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=99 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `voice`.`users`;
CREATE TABLE  `voice`.`users` (
  `UserId` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `LiferayId` int(10) unsigned NOT NULL,
  `LiferayName` varchar(75) NOT NULL,
  `LiferayIp` varchar(45) DEFAULT NULL,
  `Sip` varchar(100) DEFAULT NULL,
  `Online` tinyint(1) NOT NULL,
  `Session` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  PRIMARY KEY (`UserId`) USING BTREE,
  UNIQUE KEY `Unique` (`LiferayId`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=latin1 PACK_KEYS=1;

DELIMITER $$

DROP FUNCTION IF EXISTS `voice`.`CreateUser`$$
CREATE DEFINER=`root`@`localhost` FUNCTION  `voice`.`CreateUser`(
  nliferayId int(10),
  strLiferayName varchar(75),
  strLiferayIp varchar(45)
) RETURNS int(11)
BEGIN

SET @id = (select UserId from users where LiferayId = nliferayId LIMIT 1);

IF @id is null THEN
  insert into users (LiferayId, LiferayName, LiferayIp, Online, Session) values(nliferayId, strLiferayName, strLiferayIp, false, null);
  return 1;
END IF;

return -1;

END;

 $$

DELIMITER ;

DELIMITER $$

DROP FUNCTION IF EXISTS `voice`.`LoginUser`$$
CREATE DEFINER=`root`@`localhost` FUNCTION  `voice`.`LoginUser`(
  nliferayId int(10),
  strSession varchar(100)
) RETURNS int(11)
BEGIN

SET @id = (select UserId from users where LiferayId = nliferayId LIMIT 1);

IF @id is null THEN
  return -1;
ELSE
  update users set Online = true, Session = strSession where LiferayId = nliferayId;
  return 1;
END IF;

return -1;

END;

 $$

DELIMITER ;

DELIMITER $$

DROP FUNCTION IF EXISTS `voice`.`LogoutUser`$$
CREATE DEFINER=`root`@`localhost` FUNCTION  `voice`.`LogoutUser`(
  strSession varchar(100)
 ) RETURNS int(11)
BEGIN

  update users set Online = false, Session = null, Sip = null where Session = strSession;
  return 1;

END;

 $$

DELIMITER ;