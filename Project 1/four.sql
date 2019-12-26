select c.cid, c.name, b.cat,  sum (T.sm) cost
from (select p.title, p.year, p.cid,  sum(o.price)sm
	from yrb_purchase p, yrb_offer o
	where p.title = o.title AND p.year = o.year AND p.club = o.club
	group by p.title, p.year, p.cid) T, 
yrb_customer c, yrb_book b
where c.cid = t.cid AND b.title = t.title AND b.year = t.year
group by c.cid, c.name, b.cat
; 
