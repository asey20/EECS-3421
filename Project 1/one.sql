select distinct c.name
from yrb_customer c, yrb_purchase p
where c.cid = p.cid AND club like '_A%'
;
