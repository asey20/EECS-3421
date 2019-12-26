select distinct c.name
from yrb_customer c, yrb_member m
where c.cid = m.cid AND not exists (select *
		from yrb_purchase p
		where c.cid = p.cid AND m.club = p.club )
;
