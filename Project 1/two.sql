select distinct c.name, cc.name
from yrb_customer c, yrb_customer cc, yrb_purchase p, yrb_purchase pp
where c.cid = p.cid AND cc.cid = pp.cid AND p.title = pp.title AND p.year = pp.year AND c.cid < cc.cid
;  
